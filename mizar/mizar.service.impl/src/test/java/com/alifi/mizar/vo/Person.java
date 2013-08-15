package com.alifi.mizar.vo;


public class Person {
    
    public Person() {
        
    }
    
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    private String name;
    
    private int age;
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public int getAge() {
        return this.age;
    }

}
