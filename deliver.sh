scp $(find $(pwd) -name *-*-SNAPSHOT.jar) fdse@10.141.221.85:/home/fdse/user/issueTracker/

ssh fdse@10.141.221.85 "cd /home/fdse/user/issueTracker/ ; ./issueTracker-demo.sh stop ; sleep 2 ; ./issueTracker-demo.sh start external"