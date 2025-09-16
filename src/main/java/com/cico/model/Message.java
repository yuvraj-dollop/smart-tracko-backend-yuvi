package com.cico.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Message {
    private String name;
    private String text;
    private Date time;
}

