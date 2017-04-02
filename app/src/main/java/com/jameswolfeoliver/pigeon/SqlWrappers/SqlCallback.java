package com.jameswolfeoliver.pigeon.SqlWrappers;

import java.util.ArrayList;

public interface SqlCallback<E> {
    void onQueryComplete(ArrayList<E> results);
}
