# ---------- 构建阶段 ----------
# 使用 Maven + JDK 17 构建（项目编译目标 Java 8，JDK 17 完全兼容）
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 先拷贝 pom，利用 Docker 层缓存加速后续构建
COPY pom.xml .
COPY sky-common/pom.xml sky-common/
COPY sky-pojo/pom.xml sky-pojo/
COPY sky-server/pom.xml sky-server/
RUN mvn dependency:go-offline -q || true

# 拷贝源码并打包
COPY sky-common sky-common/
COPY sky-pojo sky-pojo/
COPY sky-server sky-server/
RUN mvn clean package -DskipTests -q

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/sky-server/target/sky-server-1.0-SNAPSHOT.jar app.jar

EXPOSE 8081
ENV TZ=Asia/Shanghai
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]