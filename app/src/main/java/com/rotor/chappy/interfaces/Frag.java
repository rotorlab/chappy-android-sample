package com.rotor.chappy.interfaces;

import com.rotor.chappy.enums.FragmentType;

public interface Frag<T>{

    FragmentType type();

    String title();

    T instance();

}
