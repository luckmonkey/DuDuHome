package com.dudu.voice.semantic.chain;

import com.dudu.android.launcher.LauncherApplication;

import com.dudu.voice.semantic.SemanticConstants;

/**
 * Created by Administrator on 2015/11/18.
 */
public class DimScreenChain extends SemanticChain  {

    @Override
    public boolean matchSemantic(String service) {
        return SemanticConstants.SERVICE_DIM.equals(service);
    }

    @Override
    public boolean doSemantic(String json) {
        return true;
    }

}