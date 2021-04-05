package com.app.ekottel.interfaces;

import com.app.ekottel.model.ImageModel;

public interface ImageInterFace {
    void updateImageSelection(ImageModel fileData, boolean update);
    void sendVideoFile(String filePath);
}
