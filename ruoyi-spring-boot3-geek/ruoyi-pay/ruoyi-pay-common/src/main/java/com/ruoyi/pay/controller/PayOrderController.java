package com.ruoyi.pay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.pay.domain.PayOrder;
import com.ruoyi.pay.service.IPayOrderService;
import com.ruoyi.pay.utils.SnowflakeIdWorker;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 订单Controller
 * 
 * @author ruoyi
 * @date 2024-02-15
 */
@RestController
@RequestMapping("/pay/order")
@Tag(name = "【订单】管理")
public class PayOrderController extends BaseController
{
    @Autowired
    private IPayOrderService payOrderService;

    @Autowired
    private SnowflakeIdWorker snowflakeidworker;

    /**
     * 查询订单列表
     */
    @Operation(summary = "查询订单列表")
    @PreAuthorize("@ss.hasPermi('pay:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(PayOrder payOrder)
    {
        startPage();
        List<PayOrder> list = payOrderService.selectPayOrderList(payOrder);
        return getDataTable(list);
    }

    /**
     * 导出订单列表
     */
    @Operation(summary = "导出订单列表")
    @PreAuthorize("@ss.hasPermi('pay:order:export')")
    @Log(title = "订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PayOrder payOrder)
    {
        List<PayOrder> list = payOrderService.selectPayOrderList(payOrder);
        ExcelUtil<PayOrder> util = new ExcelUtil<PayOrder>(PayOrder.class);
        util.exportExcel(response, list, "订单数据");
    }

    /**
     * 获取订单详细信息
     */
    @Operation(summary = "获取订单详细信息")
    @PreAuthorize("@ss.hasPermi('pay:order:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return success(payOrderService.selectPayOrderByOrderId(orderId));
    }

    /**
     * 新增订单
     */
    @Operation(summary = "新增订单")
    @PreAuthorize("@ss.hasPermi('pay:order:add')")
    @Log(title = "订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PayOrder payOrder)
    {
        payOrder.setUserId(getUserId());
        payOrder.setOrderNumber(snowflakeidworker.nextId().toString());
        AjaxResult result = toAjax(payOrderService.insertPayOrder(payOrder));
        result.put(AjaxResult.DATA_TAG, payOrder);
        return result;
    }

    /**
     * 修改订单
     */
    @Operation(summary = "修改订单")
    @PreAuthorize("@ss.hasPermi('pay:order:edit')")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PayOrder payOrder)
    {
        return toAjax(payOrderService.updatePayOrder(payOrder));
    }

    /**
     * 删除订单
     */
    @Operation(summary = "通过订单号删除订单")
    @PreAuthorize("@ss.hasPermi('pay:order:remove')")
    @Log(title = "订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/orderNumber/{orderNumber}")
    public AjaxResult removeByOrderNumber(@PathVariable( name = "orderNumber" ) String orderNumbers) 
    {
        return toAjax(payOrderService.deletePayOrderByOrderNumber(orderNumbers));
    }

    /**
     * 删除订单
     */
    @Operation(summary = "删除订单")
    @PreAuthorize("@ss.hasPermi('pay:order:remove')")
    @Log(title = "订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable( name = "orderIds" ) Long[] orderIds) 
    {
        return toAjax(payOrderService.deletePayOrderByOrderIds(orderIds));
    }
}
