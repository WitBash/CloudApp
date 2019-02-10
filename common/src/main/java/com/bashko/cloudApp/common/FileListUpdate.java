package com.bashko.cloudApp.common;

import java.util.ArrayList;
import java.util.List;

public class FileListUpdate extends AbstractMessage {
    private ArrayList<String> list;

    public FileListUpdate(ArrayList<String> list) {
        this.list = list;
    }

    public ArrayList<String> getList() {
        return list;
    }
}
