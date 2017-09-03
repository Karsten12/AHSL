package com.fonsecakarsten.ahsl;

public interface Refreshable {
    void preRefresh();

    void refresh();

    void postRefresh();
}