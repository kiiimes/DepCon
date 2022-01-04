package necon

import(
	"k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/resource"
//	"os"
	"io/ioutil"
//	"os/exec"
	"fmt"
	"strconv"
	"sync"
)

type necon struct {
	path string
	pod v1.Pod
	count int
}

var(
	n *necon
	once sync.Once
)


func GetInstance() *necon{
	once.Do(func() {
		n = &necon{}
	})

	return n
}

func (nec *necon) SetNeconPod(pod v1.Pod) error{
	namespace := pod.ObjectMeta.GetNamespace()
	if namespace != "kube-system" {
		nec.pod = pod
		nec.count++
	}
	return nil
}
/*
func (nec *necon) GetNeconPod() *v1.Pod{
	return nec.pod
}
*/
func (nec *necon) SetCgroupPath(path string) error{
	nec.path = path

	return nil
}

func (nec *necon) SetPID(containerID string,namespace string) error{

	if namespace != "kube-system" {
		//w,_ := exec.Command("docker","inspect","--format='{{.State.Pid}}'",containerID).Output()
		//위 코드가 문제인듯
		fmt.Println("%s",nec.path+"/"+containerID+"/cgroup.procs")
		pid,err:= ioutil.ReadFile(fmt.Sprintf("%s",nec.path+"/"+containerID+"/cgroup.procs"))
		err = ioutil.WriteFile(fmt.Sprintf("/proc/oslab/vif%d/pid",nec.count),[]byte(pid),0)
		if err != nil{
			fmt.Println("pid write err!")
		}
	}
	return nil
}


func (nec *necon) SetSLO() error{
	if nec.pod.ObjectMeta.GetNamespace() != ""{
		//fmt.Println("namespace : ",nec.pod.ObjectMeta.GetNamespace())
		//w := exec.Command("brctl","show")
		//w,err := ioutil.ReadFile(fmt.Sprintf("/proc/oslab/vif%d/goal",1))

		q := resource.Quantity{}
		q = nec.pod.Spec.Containers[0].Resources.Limits["example.com/SLO"]
		iq,_ := strconv.Atoi(q.String())
		aq := strconv.Itoa(iq*1000/8)
		err := ioutil.WriteFile(fmt.Sprintf("/proc/oslab/vif%d/goal",nec.count),[]byte(aq),0)

		if err != nil{
			fmt.Println("write errr!")
		}
	}
	nec.pod.Reset()

	return nil
}

