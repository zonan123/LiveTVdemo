package com.mediatek.wwtv.rxbus;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class RxBus {
    public final static RxBus instance = new RxBus();

    private final PublishSubject<Object> mSubject = PublishSubject.create();


    public void send(Object eventObj){
        mSubject.onNext(eventObj);
    }

    public<T> Observable<T> onEvent(Class<T> cls){
        return mSubject.filter(cls::isInstance).cast(cls);
    }

    public<T> Maybe<T> onFirstEvent(Class<T> cls){
        return onEvent(cls).firstElement();
    }

}
