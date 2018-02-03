package com.saintsung.saintpmc.asynctask;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by EvanShu on 2018/1/30.
 */

public class ModelFilteredFactory {
    private final static Observable.Transformer transformer = new SimpleTransformer();

    /**
     * 将Observable<BaseResponse<T>>转化Observable<T>,并处理BaseResponse
     *
     * @return 返回过滤后的Observable.
     */
    @SuppressWarnings("unchecked")
    public static <T> Observable<T> compose(Observable<BaseResponse<T>> observable) {
        return observable.compose(transformer);
    }

    /**
     * 这里就不细讲了,具体可以去看rxjava的使用.这个类的意义就是转换Observable.
     */
    private static class SimpleTransformer<T> implements Observable.Transformer<BaseResponse<T>, T> {
        //这里对Observable,进行一般的通用设置.不用每次用Observable都去设置线程以及重连设置
        @Override
        public Observable<T> call(Observable<BaseResponse<T>> observable) {
            return observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .timeout(5, TimeUnit.SECONDS)//重连间隔时间
                    .retry(5)//重连次数
                    .flatMap(new Func1<BaseResponse<T>, Observable<T>>() {
                        @Override
                        public Observable<T> call(BaseResponse<T> tBaseResponse) {
                            return flatResponse(tBaseResponse);
                        }
                    });
        }

        /**
         * 处理请求结果,BaseResponse
         *
         * @param response 请求结果
         * @return 过滤处理, 返回只有data数据的Observable
         */
        private Observable<T> flatResponse(final BaseResponse<T> response) {
            return Observable.create(new Observable.OnSubscribe<T>() {
                @Override
                public void call(Subscriber<? super T> subscriber) {
                    if (response.isSuccess()) {//请求成功
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(response.getData());
                        }
                    } else {//请求失败
                        int resultCode = response.getResultCode();
                        if (!subscriber.isUnsubscribed()) {
                            //这里抛出自定义的一个异常.可以处理服务器返回的错误.
//                            subscriber.onError(new APIException(response.getResultCode(), response.getMsg()));
                        }
                        return;
                    }
                    if (!subscriber.isUnsubscribed()) {//请求完成
                        subscriber.onCompleted();
                    }
                }
            });
        }
    }
}
