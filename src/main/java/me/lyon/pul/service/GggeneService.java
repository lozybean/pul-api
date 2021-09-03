package me.lyon.pul.service;

import me.lyon.pul.model.entity.Gggenes;
import me.lyon.pul.model.entity.PulInfo;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

public interface GggeneService {
    Gggenes plotWithBase64(PulInfo pulInfo) throws REngineException, REXPMismatchException;

    Gggenes plotWithBase64WithToken(PulInfo pulInfo, String token) throws REngineException, REXPMismatchException;
}
