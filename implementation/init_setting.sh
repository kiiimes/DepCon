#sudo rm -rf /var/lib/cni/ &&
#sudo rm -rf /var/lib/kubelet/* &&
#sudo rm -rf /etc/cni/
sudo rm -rf /var/lib/cni/ &&
sudo rm -rf /var/lib/kubelet/* &&
sudo rm -rf /run/flannel
sudo rm -rf /etc/cni/
sudo rm -rf /etc/kubernetes
sudo rm -rf /var/lib/etcd/


#docker rm -f `docker ps -aq` &&
systemctl restart docker &&
sudo rm -rf /var/lib/docker/ &&
sudo systemctl daemon-reload &&
sudo rm -rf /home/kimeunsook/.kube &&

# k8s init
#sudo kubeadm reset &&
#sudo systemctl restart kubelet &&
sudo iptables -F && sudo  iptables -t nat -F && sudo iptables -t mangle -F && sudo iptables -X


