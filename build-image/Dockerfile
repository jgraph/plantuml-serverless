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
# Install docker
RUN amazon-linux-extras install docker -y
# Copy maven settings.xml
COPY settings.xml /root/.m2/settings.xml

CMD ["bash"]
