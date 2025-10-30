package com.ruoyi.oauth.phone.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import com.ruoyi.oauth.phone.constant.DySmsConstant;
import com.ruoyi.oauth.phone.enums.DySmsTemplate;

@Component
public class DySmsUtil {
    protected final Logger logger = LoggerFactory.getLogger(DySmsUtil.class);

    @Autowired
    private DySmsConstant dySmsConfig;

    /**
     * 使用AK&SK初始化账号Client
     * 
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    private Client createClient() throws Exception {
        Config config = new Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(dySmsConfig.getAccessKeyId())
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(dySmsConfig.getAccessKeySecret());
        // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new Client(config);
    }

    /**
     * 验证参数
     * 
     * @param templateParamJson
     * @param dySmsTemplate
     * @throws Exception
     */
    private void validateParam(JSONObject templateParamJson, DySmsTemplate dySmsTemplate) {
        String keys = dySmsTemplate.getKeys();
        String[] keyArr = keys.split(",");
        for (String item : keyArr) {
            if (!templateParamJson.containsKey(item)) {
                throw new RuntimeException("模板缺少参数：" + item);
            }
        }
    }

    public void sendSms(DySmsTemplate dySmsTemplate, JSONObject templateParamJson, String phone)
            throws Exception {
        // 请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID 和
        // ALIBABA_CLOUD_ACCESS_KEY_SECRET。
        // 工程代码泄露可能会导致 AccessKey 泄露，并威胁账号下所有资源的安全性。以下代码示例使用环境变量获取 AccessKey
        // 的方式进行调用，仅供参考，建议使用更安全的 STS
        // 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html
        validateParam(templateParamJson, dySmsTemplate);
        Client client = createClient();
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(dySmsTemplate.getSignName())
                .setTemplateCode(dySmsTemplate.getTemplateCode())
                .setTemplateParam(templateParamJson.toJSONString());
        try {
            // 复制代码运行请自行打印 API 的返回值
            SendSmsResponse sendSmsResponse = client.sendSmsWithOptions(sendSmsRequest, new RuntimeOptions());
            if (sendSmsResponse.getBody().getCode().equals("OK")) {
                logger.info("短信接口返回的数据---", sendSmsResponse.getBody().getMessage());
            } else {
                logger.error("短信接口返回的数据---", sendSmsResponse.getBody().getMessage());
            }
        } catch (TeaException error) {
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            Common.assertAsString(error.message);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            Common.assertAsString(error.message);
        }
    }
}