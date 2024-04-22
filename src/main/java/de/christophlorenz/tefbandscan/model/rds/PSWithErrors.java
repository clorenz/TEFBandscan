package de.christophlorenz.tefbandscan.model.rds;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class PSWithErrors {

    final List<Pair<Character, Integer>> psWithErrors;

    public PSWithErrors() {
        this.psWithErrors = new ArrayList<>();
        init();
    }

    private void init() {
        for (int i=0; i<8; i++) {
            psWithErrors.add(Pair.of(null, 100));
        }
    }

    public void setAtPosition(int position, Character character, Integer error) {
        // We set only, if we can improve (= when the error rate is lower)
        if ((error / 10) <= (psWithErrors.get(position).getRight() / 10)) {
            psWithErrors.set(position, Pair.of(character, error));
        }
    }


    public Character getCharAtPosition(int position) {
        Character c = psWithErrors.get(position).getLeft();
        return (c != null && c != ' ') ? c : '_';
    }

    public int getErrorAtPosition(int position) {
        return psWithErrors.get(position).getRight();
    }

    public String getCssColorAtPosition(int position) {
        return "background-color: rgb(%d%%, %d%%, 0%%);".formatted(
          getErrorAtPosition(position)*33,
          100-getErrorAtPosition(position)*33
        );
    }

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer("PS{");
        for ( int i=0; i<8; i++) {
            ret.append(psWithErrors.get(i).getLeft() != null ? psWithErrors.get(i).getLeft() : " ");
            ret.append("(");
            ret.append(psWithErrors.get(i).getRight() != null ? ""+psWithErrors.get(i).getRight() : "-");
            ret.append(")");
            if (i<7) {
                ret.append(" ");
            }
        }
        ret.append("}");
        return ret.toString();
    }

    public void reset() {
        psWithErrors.clear();
        init();
    }
}
