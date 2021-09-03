package me.lyon.pul.service;

import me.lyon.pul.model.entity.PulInfo;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

public interface GggeneService {
    String plotWithBase64(PulInfo pulInfo) throws REngineException, REXPMismatchException;
}
