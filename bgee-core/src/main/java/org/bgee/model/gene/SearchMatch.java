package org.bgee.model.gene;

public abstract class SearchMatch {

    private final String term;
    
    public SearchMatch(String term) {
        this.term = term;
    }

    /**
     * @return  A {@code String} representing the matched synonym or x-ref.
     *          It is null when there is no synonym or x-ref match.
     */
    public String getTerm() {
        return term;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((term == null) ? 0 : term.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SearchMatch other = (SearchMatch) obj;
        if (term == null) {
            if (other.term != null) {
                return false;
            }
        } else if (!term.equals(other.term)) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return "SearchMatch [term=" + term + "]";
    }
}
