package com.cqsynet.swifi.scaner;

import com.cqsynet.swifi.model.ARLotteryResponseObject;

/**
 * Author: sayaki
 * Date: 2017/5/19
 */
public interface IScanFragment {

    void scanPicture();

    void showGiftAnimation();

    void updatePrize(ARLotteryResponseObject.ARLotteryResponseBody prize);
}
