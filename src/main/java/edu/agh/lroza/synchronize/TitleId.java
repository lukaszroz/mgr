package edu.agh.lroza.synchronize;

import edu.agh.lroza.common.Id;

class TitleId implements Id {
    private final String title;

    public TitleId(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TitleId) {
            return ((TitleId) obj).title.equals(title);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public String toString() {
        return "TitleId(" + title + ")";
    }
}
