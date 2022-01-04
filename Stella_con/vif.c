#include <linux/netdevice.h>
#include "common.h"
#include <linux/sched.h>
extern struct list_head off_list;
extern int tg_set_cfs_quota(struct task_group *tg,long cfs_quota_us);
extern long tg_get_cfs_quota(struct task_group *tg);

extern void (*fp_newvif)(struct net_bridge_port *p);
extern void (*fp_delvif)(struct net_bridge_port *p);
extern int (*fp_pay)(struct ancs_container *vif, struct sk_buff *skb);

struct credit_allocator *CA;
LIST_HEAD(active_vif_list);
//id for vif
int vif_cnt, counter, reset;

static struct proc_dir_entry *proc_root_dir;
static struct proc_dir_vif proc_vif[64];
int fileread = 0;
static void io_control(struct ancs_container *temp_vif){
	unsigned int perf, goal;
	int before, after, diff, dat, err, k;
       
//	if(temp_vif->remaining_credit==2){
			goal = temp_vif->max_credit;
//			perf = temp_vif->pps;
			perf = temp_vif->used_credit/1000;
//			perf = temp_vif->min_credit/1000;
			temp_vif->min_credit = perf;
//			temp_vif->min_credit = temp_vif->used_credit/1000;
/*	}
	else{
			goal = temp_vif->max_credit/10000;
			perf = temp_vif->min_credit/10000;	
	}
	*/	
	if(goal == perf || perf==0 || goal==0){
			return;
	}
		
	diff = goal - perf;

	before = tg_get_cfs_quota(temp_vif->ts->sched_task_group);

	if( perf < (goal/2) || perf > (goal*2) )
		k=5000;
	else	
		k=(5000*100000)/(goal*8);
	//	k=1000;

	if(diff>0){
		dat = ((k * diff) + (goal-1))/goal;
		if(dat>MAX_DIFF)
			dat=MAX_DIFF;
		after = before + dat;
	}
	else {
		dat = ((k*(perf-goal)) + (goal-1))/goal;
		if(dat>MAX_DIFF)
                        dat=MAX_DIFF;
		after = before - dat;
	}

	if(after > MAX_QUOTA)
		after = MAX_QUOTA;
	else if(after <= 0 || after < MIN_QUOTA)
		after = MIN_QUOTA;

	err=tg_set_cfs_quota(temp_vif->ts->sched_task_group, after);

	//printk("kwlee: c%d diff=%d, perf=%d\n", temp_vif->id, diff, perf);

	if(err)
		printk("MINKOO: %d quota setting is failed -> %d \n", temp_vif->id, after);

	return;
}
static void cpu_control(struct ancs_container *temp_vif){
	int err, period, quota;
	
	int cpu=num_online_cpus();
	int total=CA->total_weight;
	
	period = cpu * tg_get_cfs_period(temp_vif->ts->sched_task_group);

	quota = ((period * temp_vif->weight) + (total-1))/total;
	
	err = tg_set_cfs_quota(temp_vif->ts->sched_task_group, quota);

	if(err)
		printk("MINKOO: %d quota setting is failed in CPU CONTROL\n", temp_vif->id);
	else
		temp_vif->max_credit=quota;
	
	return;	
}

static void quota_control(struct timer_list *t){
	struct ancs_container *temp_vif, *next_vif;

	//int cpu = smp_processor_id();
	
	//WARN_ON(cpu != t->expires);	

	if(list_empty(&active_vif_list))
		goto out;

	list_for_each_entry_safe(temp_vif, next_vif, &active_vif_list, vif_list){

		if(!temp_vif)
			goto out;

		if(!temp_vif->ts)
			goto skip;
		
/*		if(temp_vif->remaining_credit == 1)
			cpu_control(temp_vif);
		else if(temp_vif->remaining_credit == 2 || temp_vif->remaining_credit == 3)*/
			io_control(temp_vif);

		skip:
			temp_vif->pps = 0;
			temp_vif->used_credit = 0;
		}

	out:
		mod_timer(&CA->control_timer, jiffies + msecs_to_jiffies(1000));
		counter++;	
	return;
	
}

static ssize_t vif_write(struct file *file, const char __user *user_buffer, size_t count, loff_t *ppos)
{
        const char* filename = file->f_path.dentry->d_name.name;
	struct ancs_container *vif;
	static int cnt =1;
	if(cnt>1000) return 0;
	char *temp_buf;
	temp_buf = kmalloc(count,GFP_KERNEL | __GFP_NOWARN);
	if(copy_from_user(temp_buf, user_buffer, count)){
		printk("copy fail\n");
		return 1;
	}
	char* input = strsep(&temp_buf,"\n");
	char *endptr;
	unsigned int value = simple_strtol(input, &endptr, 10);
	
        if(endptr == input && *endptr != '\0')
        {
               printk("invalid input!\n");
                return count;
        }

	vif = PDE_DATA(file_inode(file));
	if(!(vif))
	{
		printk(KERN_INFO "NULL Data\n");
		return 0;
	}

	if(!strcmp(filename, "perf"))
        {
		vif->min_credit = value;
		goto proc_out_w;
        }
	
	if(!strcmp(filename, "goal"))
        {
		vif->max_credit = value;
		goto proc_out_w;
        }

	if(!strcmp(filename, "resource_type"))
        {
		vif->remaining_credit = value;
		goto proc_out_w;
        }

	if(!strcmp(filename, "weight"))
        {
		CA->total_weight += value - vif->weight;
		vif->weight = value;
		goto proc_out_w;
        }
        if(!strcmp(filename, "pid"))
        {
                vif->pid = value;
		vif->ts = get_pid_task(find_get_pid(vif->pid), PIDTYPE_PID);
                goto proc_out_w;
        }

	printk("match failure\n");
	return count;

proc_out_w:
        if(fileread == 0){
                fileread = 1;
		return count;
        }
        else
        {
                fileread = 0;
		return 0;
        }
}

static ssize_t vif_read(struct file *file, char *buf, size_t count, loff_t *ppos)
{
	const char* filename = file->f_path.dentry->d_name.name;
	struct ancs_container *vif;	
	unsigned int len;
	char * temp_buf;
	temp_buf = kmalloc(sizeof(unsigned int),GFP_KERNEL | __GFP_NOWARN );		
	
	vif = PDE_DATA(file_inode(file));
	if(!(vif)){
                printk(KERN_INFO "NULL Data\n");
                return 0;
        }
	if(!strcmp(filename, "perf")){
		len = sprintf(temp_buf, "%u\n", vif->min_credit);
		if(copy_to_user(buf, temp_buf,strlen(temp_buf)))
			printk("MINKOO: copy fail\n");
		goto proc_out;
	}
	else if(!strcmp(filename, "goal")){
		len = sprintf(temp_buf, "%u\n", vif->max_credit);
                if(copy_to_user(buf, temp_buf,strlen(temp_buf)))
                        printk("MINKOO: copy fail\n"); 
		goto proc_out;
	}
	else if(!strcmp(filename, "weight")){
		len = sprintf(temp_buf, "%u\n", vif->weight);
                if(copy_to_user(buf, temp_buf,strlen(temp_buf)))
                        printk("MINKOO: copy fail\n"); 
		goto proc_out;
	}
	else if(!strcmp(filename, "resource_type")){
		len = sprintf(temp_buf, "%u\n", vif->remaining_credit);
                if(copy_to_user(buf, temp_buf,strlen(temp_buf)))
                        printk("MINKOO: copy fail\n"); 
		goto proc_out;
	}
	else if(!strcmp(filename, "pid")){
                len = sprintf(temp_buf, "%u\n", vif->pid);
                if(copy_to_user(buf, temp_buf,strlen(temp_buf)))
                        printk("MINKOO: copy fail\n");
                goto proc_out;
        }
	else{
		count = sprintf(buf, "%s", "ERROR");
		return count;
	}
proc_out:

	if(fileread == 0){
                fileread = 1;
                return len;
        }
        else
        {
                fileread = 0;
                return 0;
        }
}


static const struct file_operations vif_opt ={
	.write = vif_write,
	.read = vif_read,
};

int pay_credit(struct ancs_container *vif, struct sk_buff *skb){
	vif->pps++;
	vif->used_credit+=skb->len;
	return PAY_SUCCESS;
}

void new_vif(struct net_bridge_port *p){
	int idx;
	
	if(p==NULL){
		printk(KERN_ERR "MINKOO: new port pointer null err\n");
		return;
	}
	
	//initialize new ancs_container
	struct ancs_container *vif;
	vif = kmalloc(sizeof(struct ancs_container), GFP_KERNEL | __GFP_NOWARN);
	INIT_LIST_HEAD(&vif->vif_list);
	vif->need_reschedule = false;
	vif->weight = 1;	//0 is arbitary value, give weight to check QOS algorithm working.
	vif->min_credit = 0;		//arbitary
	vif->max_credit = 0;		//arbitary
	vif->remaining_credit = 0; 
	vif->used_credit = 0;	
	vif->id = vif_cnt++;
	vif->ts = NULL;
	vif->p =p;
	p->vif=vif;

	//add vif to credit allocator list
	list_add(&vif->vif_list, &active_vif_list);

	//update function for credit allocator
	CA->total_weight += vif->weight;
        CA->num_vif++;

	//need to implement: proc_fs new
	idx = vif->id;
	proc_vif[idx].id = (int)vif->id;
	sprintf(proc_vif[idx].name, "vif%d", (int)vif->id);
	proc_vif[idx].dir = proc_mkdir(proc_vif[idx].name, proc_root_dir);
	proc_vif[idx].file[0] = proc_create_data("perf",0600, proc_vif[idx].dir, &vif_opt, vif);
	proc_vif[idx].file[1] = proc_create_data("goal",0600, proc_vif[idx].dir, &vif_opt, vif);
	proc_vif[idx].file[2] = proc_create_data("weight",0600, proc_vif[idx].dir, &vif_opt, vif);
	proc_vif[idx].file[3] = proc_create_data("resource_type",0600, proc_vif[idx].dir, &vif_opt, vif);
	proc_vif[idx].file[5] = proc_create_data("pid",0600, proc_vif[idx].dir, &vif_opt, vif);

	printk(KERN_INFO "MINKOO: new vif%d weight=%d, min=%d, max=%d\n", vif->id, vif->weight, vif->min_credit, vif->max_credit);
}

void del_vif(struct net_bridge_port *p){
	int idx;

	if(p == NULL){
		printk(KERN_ERR "MINKOO: del port pointer null\n");
		return;
	}
	else if(p->vif == NULL){
		printk(KERN_ERR "MINKOO: del vif pointer null\n");
                return;
	}
	else printk(KERN_INFO "MINKOO: delete vif%d\n", p->vif->id);
	
	//delete list from credit allocator
	list_del(&p->vif->vif_list);
	
	//update function for credit allocator
	CA->total_weight -= p->vif->weight;
        CA->num_vif--;

	//need to implerment: proc_fs del	
	idx = p->vif->id;
	remove_proc_entry("perf", proc_vif[idx].dir);
	remove_proc_entry("goal", proc_vif[idx].dir);
	remove_proc_entry("weight", proc_vif[idx].dir);
	remove_proc_entry("resource_type", proc_vif[idx].dir);
	remove_proc_entry("pid", proc_vif[idx].dir);
	remove_proc_entry(proc_vif[idx].name, proc_root_dir);
	
	//free memory		
	kfree(p->vif);
	p->vif=NULL;
}

static int __init vif_init(void)
{
	int cpu = smp_processor_id();
	struct ancs_container *vif, *next_vif;
	vif_cnt = 1;
	counter = 0;	

	//function pointer linking
	fp_pay = &pay_credit;
	//fp_pay = NULL;
	fp_newvif = &new_vif;
	fp_delvif = &del_vif;	

	//credit allocator initialization
	CA = kmalloc(sizeof(struct credit_allocator), GFP_KERNEL | __GFP_NOWARN);
	if (!CA)
		return -ENOMEM;	
	CA->total_weight = 0;
	CA->credit_balance = 0;
	CA->num_vif =0;
	INIT_LIST_HEAD(&CA->active_vif_list);
	spin_lock_init(&CA->active_vif_list_lock);

	//make proc directory
	proc_root_dir = proc_mkdir("oslab", NULL);

	timer_setup(&CA->control_timer, quota_control, cpu );
       mod_timer(&CA->control_timer, jiffies + msecs_to_jiffies(1000));

	printk(KERN_INFO "MINKOO: credit allocator init!!\n");	

	return 0;
}

static void __exit vif_exit(void)
{
	//struct list_head *p, *temp;
	struct ancs_container *vif, *next_vif;

	printk(KERN_INFO "MINKOO: credit allocator exit!!\n");

	if(list_empty(&active_vif_list))
		goto out;	

	//need to implement : traverse CA list and add to off_list
	list_for_each_entry_safe(vif, next_vif, &active_vif_list, vif_list){
	//	vif = list_entry(p, struct ancs_container, vif_list);
		printk("MINKOO: delvif%d\n", vif->id);
		del_vif(vif->p);
	//	INIT_LIST_HEAD(&vif->off_list);
	//	list_add(&vif->off_list, &off_list);
	}

out:
	remove_proc_entry("oslab", NULL);

#if 0
	//delete timer
	del_timer(&CA->account_timer);
#endif
	del_timer(&CA->control_timer);

	//free ca
	kfree(CA);
	return;
}

module_init(vif_init);
module_exit(vif_exit);

MODULE_AUTHOR("Korea University");
MODULE_DESCRIPTION("OSLAB");
MODULE_LICENSE("GPL v2");
MODULE_VERSION("ver 1.0");
