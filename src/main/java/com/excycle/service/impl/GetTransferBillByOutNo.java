package com.excycle.service.impl;


import static com.excycle.service.impl.TransferService.client;

/**
 * 商户单号查询转账单
 */
public class GetTransferBillByOutNo {

  public static void main(String[] args) {
//    // TODO: 请准备商户开发必要参数，参考：https://pay.weixin.qq.com/doc/v3/merchant/4013070756

    TransferService.GetTransferBillByOutNoRequest request = new TransferService.GetTransferBillByOutNoRequest();
    request.outBillNo = "plfk2020042013";
    TransferService.TransferBillEntity response = client.run(request);
    // TODO: 请求成功，继续业务逻辑
    System.out.println(response);
  }

  
}