**前言**：
本文的源码是根据一位前辈的帖子加入了本人的理解总结的，如果感觉不错，感谢采用，如果有瑕疵，请及时沟通，我会尽快修复不足。只是喜欢代码，谈不上精通。

**正文**：

 1. 导入依赖
 	
	项目gradle

```
		allprojects {
		    repositories {
		       ...
		        maven { url 'https://jitpack.io' }
		    }
		}
```

app的gradle
		
```
		implementation 'com.github.xiangshu957:mvp:0.0.3'
```
以下是成熟的第三方依赖库，本人暂时还没有明白为什么不导入下面的依赖会有问题，因为我自己依赖中已经导过了，如果不导的话会出问题，小伙伴们还是受累导一下哈。
```

	    //RxJava + Retrofit
	    implementation 'com.squareup.okhttp3:okhttp:4.7.2'
	    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
	    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
	    implementation 'com.squareup.retrofit2:adapter-rxjava2:2.4.0'
	    implementation 'io.reactivex.rxjava2:rxjava:2.2.3'
	    implementation 'io.reactivex.rxjava2:rxandroid:2.1.0'
	    implementation 'com.squareup.okhttp3:logging-interceptor:4.7.2'
	
	    //放着没有及时回收造成RxJava内存泄漏
	    implementation 'com.trello.rxlifecycle2:rxlifecycle-components:2.2.2'
	
	    //引入dagger.android库
	    implementation 'com.google.dagger:dagger-android:2.35.1'
	    // if you use the support libraries
	    implementation 'com.google.dagger:dagger-android-support:2.24'
	    annotationProcessor 'com.google.dagger:dagger-compiler:2.24'
	    annotationProcessor 'com.google.dagger:dagger-android-processor:2.24'
	
	    implementation 'com.jakewharton:butterknife:8.2.1'
	    annotationProcessor 'com.jakewharton:butterknife-compiler:8.2.1'
```

 2. Application中的初始化
 		
```java
		public class MyApplication extends DaggerApplication {
	
		    private static MyApplication context;
		
		    @Override
		    public void onCreate() {
		        super.onCreate();
		        context = this;
		
				//sp工具类初始化，在数据缓存的时候有用到，也可以用于你自己的业务场景
		        PreferenceUtil.getInstance().init(this, "test_sp");
		        //retrofit的初始化（baseUrl,图片下载和上传的接口地址集合，写网络请求方法的接口的反射类对象）
				//图片接口地址的集合是由于打印请求信息的时候不能打印图片的信息，做了一个过滤器，可空
		        RetrofitManager.getInstance(SysCommon.BASE_URL, null, RetrofitApi.class);
		    }
		
		    @Override
		    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
		        return DaggerAppComponent.builder().application(this).build();
		    }
		
		    public static Context getContext() {
		        return context;
		    }
	}	
		
```

 3. RetrofitApi接口书写
 
 	RetrofitApiService 是我自己写的一个接口，用来初始化Retrofit，所以自定义RetrofitApi一定要extends
```java
		public interface RetrofitApi extends RetrofitApiService {
		
		    @GET(SysCommon.YM_RUL)
		    Observable<实体类|字符串|返回的具体结果> getData();
		
		}
```


 5. Dagger注入
 

```java
		@Component(modules = {AndroidSupportInjectionModule.class})
		public interface AppComponent extends AndroidInjector<MyApplication> {
		
		    @Component.Builder
		    interface Builder{
		        @BindsInstance
		        Builder application(Application application);
		        AppComponent build();
		    }
		
		}
```

 
 7. Contract契约接口
	这里简单的以MainActivity为例
```java
		public interface MainContract {
		
		    interface View extends BaseView{
				/**
				这里都是更新Ui的操作
				例如网络请求成功或失败之后调用的方法
				此处只需要传参，具体实现在activity中实现
				**/
		        void getDataSuccess(TestBean dataJson);
		        void getDataFail(String message);
		
		    }
		
		    interface Presenter {
		
				/**
				此处是一些业务逻辑
				例如：网络请求
				**/
		        void getData();
		
		    }
		
		}
```

 8. Presenter业务逻辑类
 
	presenter中是获取数据的具体实现，简单的来说：MainContract.Presenter中的方法由presenter实现，并且和MainContract.View进行数据传递，一起往下看吧，都在注释里：
```java
		public class MainPresenter extends BasePresenter<MainContract.View> implements MainContract.Presenter {
		
		    @Inject
		    public MainPresenter() {
		
		    }
		
		
		    @SuppressLint("CheckResult")
		    @Override
		    public void getData() {
		    	//这两个是缓存的时间，源码里面有感兴趣的可以去看看
		        setOfflineCacheTime(0);
		        setOnlineCacheTime(0);
		        /**
		        	此处是真正的网络请求了
		        	observe()是封装的方法，有源码的，不过多介绍了
		        **/
		        observe(((RetrofitApi) apiService()).getData(), false)
		                .subscribe(s -> {
		                	//请求成功之后通过view中的方法传递请求到的数据
		                	//此处可以对数据进行是否为空的判断或者返回结果的判断，建议做到成功必定是有数据的成功，而不仅仅是请求成功
		                    getView().getDataSuccess(s);
		                }, throwable -> {
		                	//请求失败之后通过view中方法传递失败的原因
		                	//此处可以添加一些判断，因为有可能是超时，也有可能是证书验证等问题，不然直接提示exception吗？
		                    getView().getDataFail(throwable.getMessage());
		                });
		
		
		    }
		}
```

 10. activity中的填空题
 
		到了activity中基本都是填空题了，BaseActivity<T>的泛型是继承了BasePresenter的presenter，并且实现MainContract.View，这样就拥有了presenter的使用权，同时也能知道presenter在工作之后的结果了
```java
		public class MainActivity extends BaseActivity<MainPresenter> implements MainContract.View {
		
			//获取presenter对象
		    @Override
		    public MainPresenter createPresenter() {
		        return new MainPresenter();
		    }
			
			//获取布局文件
		    @Override
		    public int getContentViewId() {
		        return R.layout.activity_main;
		    }
		
			//设置监听事件
		    @Override
		    public void setListener() {
		        mPresenter.getData();
		    }
			
			//业务逻辑处理
		    @Override
		    public void processLogic() {
		
		    }
		
			//注册广播
		    @Override
		    public void registerReceiver() {
		
		    }
			
			//注销广播
		    @Override
		    public void unRegisterReceiver() {
		
		    }
			
			//网络请求中可以弹框提示一下
		    @Override
		    public void showLoading(String msg) {
		
		    }
		
			//请求结束之后记得收回弹框
		    @Override
		    public void hideLoading() {
		
		    }
			
			//这是用来测试的方法
			//网络请求成功之后回调用这个方法，此时的数据就是你想要的数据，你可以操作数据了
		    @Override
		    public void getDataSuccess(TestBean dataJson) {
		        LogUtils.e(GsonUtils.ser(dataJson));
		    }
		
			//请求失败或者数据异常会调用次方法
		    @Override
		    public void getDataFail(String message) {
		        LogUtils.e(message);
		    }
		}
```

[源码地址](https://github.com/xiangshu957/mvp.git)

**最后，感谢大家的阅读，此文只是初版，语言能力一般，模板我也尽力了，有些说的不对的地方还请多多包涵，源码后续会更新出来，如果有什么疑问或者建议，欢迎评论区留言**







