package com.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "data")
public class DataResponse {
    @Transient
    public static final String SEQUENCE_NAME = "data_sequence";

    @Id
    private long id;
    private String someData;
    private String status;

}