package com.tiny_job.admin.exception;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-16 22:09
 **/
public class TinyJobExceptionAssert {

    public static void notNull(Object object, String msg) {
        if (object == null) {
            throw new TinyJobException(msg);
        }
    }
}
