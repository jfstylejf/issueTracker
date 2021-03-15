rm -rf result
rm -rf tokenData
java -jar CloneManager.jar dataset=C:\\Users\\86189\\Desktop\\clone\\IssueTracker-Master language=java extensions=java granularity=method exe=executable/executable_cpu_win10.exe threshold=0.7
#java -jar CloneManager.jar dataset=D:\\work\\test\\dataset\\java\\tomcat language=java extensions=java granularity=snippet exe=executable/executable_cpu_win10_snippet.exe threshold=0.7