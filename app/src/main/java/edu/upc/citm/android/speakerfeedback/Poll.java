package edu.upc.citm.android.speakerfeedback;

import android.util.Log;

import java.util.Date;
import java.util.List;

public class Poll {
    private String question;
    private List<String> options;
    private boolean open;
    private Date start, end;
    private List<Integer> results;

    Poll(){}

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getOptionsAsString(){
        // Així es sumen molts strings en Java
        StringBuilder b = new StringBuilder();
        for (String opt : options) {
            b.append(opt);
            b.append("\n");
        }
        return b.toString();
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public List<Integer> getResults() {
        return results;
    }

    public void setResults(List<Integer> results) {
        this.results = results;
    }

    public String getResultsAsString(){
        StringBuilder b = new StringBuilder();
        for (Integer opt : results) {
            b.append(opt.toString());
            b.append("\n");
        }
        return b.toString();
    }
}
