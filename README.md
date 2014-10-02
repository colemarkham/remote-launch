docker-launch
=============

The Docker launch Eclipse plug-in provides a Java VM installer plugin for JDT that lets you define
a JRE that will actually run docker to start up a container to run your Java application.

This work started from my own personal need to use Eclipse on my Mac but run the Java application
I'm building in a Docker container running in my boot2docker VM. So while this should work in other
environments I can't promise anything until it gets tested there. So consider this BETA quality
and report anything you find in the issues in my github repo.

First a word on the environment you need. The idea is that you are editing and building your Java
projects in Eclipse on the host. You need to be able to share all the files needed to run your
application into your Docker container. For boot2docker, that means you need to first share your
files into the VirtualBox VM. That means using a boot2docker image that supports the folder sharing
in the VirtualBox Guest Additions. Google around to find instructions on how to set that up.
I have /Users mounted as /Users in the VM which is what they generally recommend and for good reason.
Then from there, you share your folders into the Docker container by specifying volumes. More on that
in a minute.

Now to set up the JRE in Eclipse, you need to have a copy of the JRE local on your host that Eclipse
can get at. You can use the docker cp command to copy it out of your container. Or you can do what I
did and download the JRE from Oracle and then use docker build to create an image with that
JRE in it.

Now, it's probably time to install the plug-in. I have the feature up on bintray. Open the Eclipse
Install New Software... dialog and use the following URL and install the single feature that's there:

http://dl.bintray.com/cdtdoug/docker-launch

After you restart, go into your Eclipse Preferences and select Java -> Installed JREs. Click Add...
You'll see Docker Remote JRE there. Select it and click next. In the JRE home field, put the
location where you extracted the JRE on your host. You should see the system libraries show up. If you're interested
I'm actually reusing the Standard VM type which gives us alot of things like that for free. Next give
it a name, probably something with the word docker in it so you can tell it apart from the local JREs.

Now comes the most complicated part. You could enter the VM arguments in every Java launch configuration
you make, or you can enter them here in the Default VM arguments. Highly recommended to do it here. So, the idea is
that the arguments you put here are very magic. They come in three segments, first are arguments I use to
convert the java arguments to run in the docker image, second are arguments to docker to run the
container, and third are any default java arguments, which there usually aren't.

My arguments are first and are as follows:

    -h - the IP address of the host as seen from the Docker container
    -t hostPath:dockerPath - used to translate host paths to docker paths

The IP address is important since Eclipse starts up and expects the JRE to connect to it. Now if you
can't see your host from the Docker image then we've got a problem. And I have a feeling we will
and will eventually need to do a launch configuration type similar to Java Remote Application that
will make this work. But later.

The -t options are to allow us to map the paths that Eclipse sees and puts onto the Java classpath
arguments into paths that are mounted in the docker container. It could be easier to just mount
/Users into your VM and then -v them into your Docker container with the same name. but this gives
you the flexibility to do what you need to do.

Now after you specify my options put a double minus, --. That signals that we're ready for docker
arguments. For my boot2docker image, I have to put the -H option to tell docker to connect to the
docker server running in the VM. I then use the run command to mount my folders into the container
and start up my image.

Follow your docker command with another double minus, --, to signal it's time for Java arguments.
Usually there won't be any by default and you'll put them as VM arguments in your launch
configurations. But you need to put this final -- here anyway or make sure the first argument
in your launch config is --, which is weird. Just put it here to be safe.

Here's the Default VM arguments I used for testing using my dougtest image which contains the JRE
I'm using:

    -h 10.0.2.2 -t /Users:/host -- -H tcp://192.168.59.103:2375 run --rm -v /Users:/host dougtest --

If I had used -v /Users:/Users then I wouldn't need the -t option. 10.0.2.2 is the IP address for
the host when running inside VirtualBox. 192.168.59.103 is the IP address my Mac has for the VM.

Now, create a new Java project. Create a launch configuration for it. Make sure your JRE is set to
the docker JRE. And go for it. Please raise any issues you have here on github and I'll try to
answer or fix them.
