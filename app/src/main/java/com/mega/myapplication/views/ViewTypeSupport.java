package com.mega.myapplication.views;

public interface ViewTypeSupport <T> {
    int getItemLayoutId(int itemType);
    int getItemType(T t, int position);
}
