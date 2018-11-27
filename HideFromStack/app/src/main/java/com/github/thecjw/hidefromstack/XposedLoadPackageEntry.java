package com.github.thecjw.hidefromstack;

import android.content.Context;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import lombok.val;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class XposedLoadPackageEntry implements IXposedHookLoadPackage {
  private static final String TARGET_PACKAGE_NAME = "";
  private String packageName = "";
  private String processName = "";
  private Context applicationContext = null;

  public XposedLoadPackageEntry() {
    //
  }

  private boolean checkCaller(final StackTraceElement element) {
    // Remove Xposed from caller.
    if (element.getClassName().startsWith("de.robv.android.xposed")) {
      return true;
    }

    // Or dexposed.
    if (element.getClassName().startsWith("com.taobao.android.dexposed")) {
      return true;
    }

    // Even any classes/methods/sources contains "hook".
    if (element.toString().toLowerCase().contains("hook")) {
      return true;
    }

    return false;
  }

  @Override
  public void handleLoadPackage(final LoadPackageParam loadPackageParam)
      throws Throwable {

    findAndHookMethod(Throwable.class,
        "getInternalStackTrace",
        new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
            val oldCallStack = (StackTraceElement[]) param.getResult();
            val newCallStack = new ArrayList<>();
            for (StackTraceElement element : oldCallStack) {
              if (checkCaller(element)) {
                continue;
              }
              newCallStack.add(element);
            }

            param.setResult(newCallStack.toArray(new StackTraceElement[newCallStack.size()]));
          }
        });
  }
}
