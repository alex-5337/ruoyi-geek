package com.ruoyi.pay.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 订单对象 pay_order
 * 
 * @author ruoyi
 * @date 2024-02-15
 */
@Schema(title = "订单对象")
public class PayOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    @Schema(title = "订单ID")
    private Long orderId;

    /** 用户id */
    @Schema(title = "用户id")
    @Excel(name = "用户id")
    private Long userId;

    /** 订单号 */
    @Schema(title = "订单号")
    @Excel(name = "订单号")
    private String orderNumber;

    /** 订单状态 */
    @Schema(title = "订单状态")
    @Excel(name = "订单状态")
    private String orderStatus;

    /** 订单总金额 */
    @Schema(title = "订单总金额")
    @Excel(name = "订单总金额")
    private String totalAmount;

    /** 订单内容 */
    @Schema(title = "订单内容")
    @Excel(name = "订单内容")
    private String orderContent;

    /** 订单备注 */
    @Schema(title = "订单备注")
    @Excel(name = "订单备注")
    private String orderRemark;

    /** 负载信息 */
    @Schema(title = "负载信息")
    @Excel(name = "负载信息")
    private String orderMessage;

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setOrderContent(String orderContent) {
        this.orderContent = orderContent;
    }

    public String getOrderContent() {
        return orderContent;
    }

    public void setOrderRemark(String orderRemark) {
        this.orderRemark = orderRemark;
    }

    public String getOrderRemark() {
        return orderRemark;
    }

    public void setOrderMessage(String orderMessage) {
        this.orderMessage = orderMessage;
    }

    public String getOrderMessage() {
        return orderMessage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("orderId", getOrderId())
                .append("userId", getUserId())
                .append("orderNumber", getOrderNumber())
                .append("orderStatus", getOrderStatus())
                .append("totalAmount", getTotalAmount())
                .append("orderContent", getOrderContent())
                .append("orderRemark", getOrderRemark())
                .append("orderMessage", getOrderMessage())
                .toString();
    }
}
