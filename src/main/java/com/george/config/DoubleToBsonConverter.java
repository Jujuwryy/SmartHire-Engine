package com.george.config;

import org.bson.BsonDouble;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class DoubleToBsonConverter implements Converter<Double, BsonDouble> {

    @Override
    public BsonDouble convert(Double source) {
        return new BsonDouble(source);
    }
}
