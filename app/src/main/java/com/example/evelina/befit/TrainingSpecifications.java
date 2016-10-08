package com.example.evelina.befit;

/**
 * Created by User on 04-Oct-16.
 */


import com.example.evelina.befit.model.Exercise;

import java.util.List;

/**
 * Class for holding the challenge's times of completion and last day of completion.Needed for expandable recycler view.
 */
public class TrainingSpecifications  {
    private int mTimesCompleted ;
    private String mDateLastCompletion;
    //here we add the list of exercises
    List<Exercise> exercises;


    public TrainingSpecifications(int mTimesCompleted, String mDateLastCompletion,List<Exercise> exercises) {
        this.mTimesCompleted = mTimesCompleted;
        this.mDateLastCompletion = mDateLastCompletion;
        this.exercises = exercises;
    }


    public int getmTimesCompleted() {
        return mTimesCompleted;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public String getmDateLastCompletion() {
        return mDateLastCompletion;
    }
}