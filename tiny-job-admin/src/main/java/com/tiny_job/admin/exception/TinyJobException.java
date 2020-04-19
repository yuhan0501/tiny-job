package com.tiny_job.admin.exception;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-16 21:50
 **/
public class TinyJobException extends RuntimeException {
    private static final long serialVersionUID = 1902621202146570399L;

    public TinyJobException() {
        super();
    }

    public TinyJobException(String message) {
        super(message);
    }
}
