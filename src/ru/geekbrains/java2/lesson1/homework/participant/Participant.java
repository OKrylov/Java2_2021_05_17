package ru.geekbrains.java2.lesson1.homework.participant;


import ru.geekbrains.java2.lesson1.homework.obstacle.Obstacle;

public interface Participant {

    int run();

    int jump();

    default boolean passObstacle(Obstacle obstacle) {
        return doAction(obstacle);
    }

    private boolean doAction(Obstacle obstacle) {
        return obstacle.passObstacleBy(this);
    }

}
