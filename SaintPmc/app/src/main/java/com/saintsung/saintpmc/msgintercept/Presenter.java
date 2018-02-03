package com.saintsung.saintpmc.msgintercept;

/**
 * Created by EvanShu on 2018/2/1.
 */

public class Presenter implements IPresenter{
    private IView mView;
    public Presenter(IView view){
        this.mView=view;
    }
    @Override
    public void search() {
        String inputString=mView.getSearchString();
        if(inputString.isEmpty()){
            return;
        }
        int hashCode=inputString.hashCode();
        IUserService userService=new UserService();
        String result="Result:"+inputString+"-"+userService.search(hashCode);
        mView.setSearchString(result);
    }
}
