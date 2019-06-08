package com.alt.karman.wifimanager;

import java.util.ArrayList;

public interface FinishScanListener {
    void onFinishScan(ArrayList<ClientScanResult> clients);
}