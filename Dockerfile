#基于jdk8创建
FROM openjdk:17-jdk-alpine
#复制文件到容器
COPY wechatter-0.0.1-SNAPSHOT.jar app.jar
#挂载目录
#VOLUME /mydata/docker-tmp
#解决中文乱码
ENV LANG C.UTF-8

#容器创建开始即启动程序
#CMD java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar app.jar --server.port=4001

ENTRYPOINT ["nohup" ,"java", "-jar", "app.jar", ">> /log/app-$(date +%Y-%m-%d).log 2>&1 &" ,"--server.port=4001" ,"-Xms256m -Xmx256m"]
#暴露端口
EXPOSE 4001
