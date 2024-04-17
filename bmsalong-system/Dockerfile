FROM openjdk:11

## 创建目录，并使用它作为工作目录
RUN mkdir -p /bmsalong
WORKDIR /bmsalong
## 将后端项目的 Jar 文件，复制到镜像中
COPY ./target/bmsalong-system.jar bmsalong.jar

## 设置 JAVA_OPTS 环境变量，可通过 docker run -e "JAVA_OPTS=" 进行覆盖
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=dev

## 暴露后端项目的 7999 端口
EXPOSE 7999

## 启动后端项目
CMD java ${JAVA_OPTS} -jar bmsalong.jar
