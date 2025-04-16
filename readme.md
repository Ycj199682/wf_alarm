mvn package后 若target中没有lib，需要复制进去
lib与jar包一级
执行jar包：java -Dloader.path=./lib -jar wf_alarm-1.0-SNAPSHOT-jar-with-dependencies.jar