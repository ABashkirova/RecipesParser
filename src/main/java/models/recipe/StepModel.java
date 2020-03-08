package models.recipe;

import org.jetbrains.annotations.NotNull;

public class StepModel {
    private String number;
    private String description;
    private String imageStep;

    public StepModel(String number, String description, String imageStep) {
        setNumber(number);
        setDescription(description);
        setImageStep(imageStep);
    }
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageStep() {
        return imageStep;
    }

    public void setImageStep(String imageStep) {
        this.imageStep = imageStep;
    }

    @NotNull
    public Boolean stepWithoutImage() {
        return getImageStep().isEmpty();
    }
    @NotNull
    public Boolean stepWithoutDescription() {
        return getDescription().isEmpty();
    }
    @NotNull
    public Boolean stepDescriptionContains(String mess) {
        return getDescription().contains(mess);
    }
}
