package com.google.android.tagmanager.examples.cuteanimals;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.TagManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CuteActivity extends Activity {

    private static final long TIMEOUT_FOR_CONTAINER_OPEN_MILLISECONDS = 10000;
    private static final String CONTAINER_ID = "GTM-PCBBDK";

    TextView txtTitle, txtReturnedValue;
    private int numCalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cute);

        txtTitle = (TextView) findViewById(R.id.title);
        txtReturnedValue = (TextView) findViewById(R.id.txtreturnedvalue);

        TagManager tagManager = TagManager.getInstance(this);

        PendingResult<ContainerHolder> pending =
                tagManager.loadContainerPreferNonDefault(CONTAINER_ID,
                        R.raw.gtm_analytics);

        // The onResult method will be called as soon as one of the following happens:
        //     1. a saved container is loaded
        //     2. if there is no saved container, a network container is loaded
        //     3. the 2-second timeout occurs
        pending.setResultCallback(new ResultCallback<ContainerHolder>() {
            @Override
            public void onResult(ContainerHolder containerHolder) {
                ContainerHolderSingleton.setContainerHolder(containerHolder);
                Container container = containerHolder.getContainer();
                if (!containerHolder.getStatus().isSuccess()) {
                    Log.e("CuteAnimals", "failure loading container");
                    return;
                }
                ContainerLoadedCallback mCallback = new ContainerLoadedCallback();
                mCallback.registerCallbacksForContainer(container);
                containerHolder.setContainerAvailableListener(mCallback);
            }
        }, TIMEOUT_FOR_CONTAINER_OPEN_MILLISECONDS, TimeUnit.MILLISECONDS);


    }




    private class ContainerLoadedCallback implements ContainerHolder.ContainerAvailableListener {
        @Override
        public void onContainerAvailable(ContainerHolder containerHolder, String containerVersion) {
            // We load each container when it becomes available.
            Container container = containerHolder.getContainer();
            registerCallbacksForContainer(container);
        }

        public void registerCallbacksForContainer(Container container) {
            // Register two custom function call macros to the container.
            container.registerFunctionCallMacroCallback("increment", new CustomMacroCallback());
            container.registerFunctionCallMacroCallback("mod", new CustomMacroCallback());
        }
    }

    class CustomMacroCallback implements Container.FunctionCallMacroCallback {


        @Override
        public Object getValue(String functionName, final Map<String, Object> parameters) {
            Log.e("Parameters", parameters.toString());
            if ("mod".equals(functionName)) {
                Log.e("CuteAnimals", parameters.toString());
                Toast.makeText(CuteActivity.this, parameters.get("modkey").toString(), Toast.LENGTH_SHORT).show();

                        txtReturnedValue.setText("Returned Value from the function is " + parameters.get("modkey").toString());


                return 1;
            } else if ("increment".equals(functionName)) {
                Log.e("CuteAnimals", parameters.toString());


                        txtTitle.setText(parameters.get("key").toString());

                return ++numCalls;
            } else {
                throw new IllegalArgumentException("Custom macro name: " + functionName + " is not supported.");
            }


        }
    }

    public void refreshButtonClicked(View view) {
        Log.i("CuteActivity", "refreshButtonClicked");


        // Push the "refresh" event to trigger firing an function.
        TagManager.getInstance(this).getDataLayer().push("event", "refresh");
        ContainerHolderSingleton.getContainerHolder().refresh();


    }

}
