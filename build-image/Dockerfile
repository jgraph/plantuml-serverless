FROM amazoncorretto:11

LABEL maintainer="JGraph Ltd"

RUN yum update -y && \
    yum install tar -y && \
    yum install git -y && \
    yum install ant -y && \
    yum install maven -y && \
    yum install which -y

RUN curl --silent --location https://rpm.nodesource.com/setup_14.x | bash - && \
    yum install nodejs -y

# Install docker and serverless
RUN amazon-linux-extras install docker -y && \
    npm install -g serverless

RUN (cd /lib/systemd/system/sysinit.target.wants/; for i in *; do [ $i == systemd-tmpfiles-setup.service ] || rm -f $i; done); \
    rm -f /lib/systemd/system/multi-user.target.wants/*;\
    rm -f /etc/systemd/system/*.wants/*;\
    rm -f /lib/systemd/system/local-fs.target.wants/*; \
    rm -f /lib/systemd/system/sockets.target.wants/*udev*; \
    rm -f /lib/systemd/system/sockets.target.wants/*initctl*; \
    rm -f /lib/systemd/system/basic.target.wants/*;\
    rm -f /lib/systemd/system/anaconda.target.wants/*;

# Copy maven settings.xml and aws credentials files
COPY settings.xml /root/.m2/settings.xml
COPY credentials /root/.aws/credentials

VOLUME [ "/sys/fs/cgroup" ]
CMD ["/usr/sbin/init"]
