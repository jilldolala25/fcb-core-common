package tw.com.fcb.dolala.core.common.web;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import tw.com.fcb.dolala.core.common.http.Response;
import tw.com.fcb.dolala.core.common.web.dto.BankAddressDto;
import tw.com.fcb.dolala.core.common.web.dto.BankDto;
import tw.com.fcb.dolala.core.common.web.dto.CustomerDto;

/**
 * @author sinjen
 * 
 */
@RestController
//@RequestMapping("/common")
public interface CommonApi {

	// 匯率處理
	@GetMapping("/get-fxrate/{exchg-rate-type}/{currency}/{standard-currency}")
	@Operation(description = "依exchgRateType, currency, standardCurrency取得ExchgRate", summary = "讀取買/賣匯匯率")
	public BigDecimal isGetFxRate(@PathVariable("exchg-rate-type") String exchgRateType, @PathVariable("currency") String currency, @PathVariable("standard-currency") String standardCurrency);
	
	@GetMapping("/check-fxrate")
	@Operation(description = "檢核承作匯率", summary = "檢核承作匯率")
	public boolean isCheckFxRate(BigDecimal exchgRate);

	// 國家資料處理
	@GetMapping("/{countryCode}/get-country-name")
	@Operation(description = "以國家代號英文2碼讀取國家代號4碼數字", summary = "讀取國家代號4碼數字")
	public String isGetCountryNumber(String countryCode);
		
	@GetMapping("/{countryNumber}/get-country-code")
	@Operation(description = "以國家代號4碼數字讀取國家代號英文2碼", summary = "讀取國家代號英文2碼")
	public String isGetCountryCode(String countryNumber);
	
	// 身分證號檢核
	@GetMapping("/{id}/check-id")
	@Operation(description = "檢核居留證或統一證號是否符合編碼規則", summary = "身分證號檢核")
	public boolean isCheckId(String id);

	// 讀取取號檔
	@GetMapping("/{systemType}/{branch}/get-number-serial")
	@Operation(description = "查詢取號檔資訊", summary = "BY業務別查詢取號檔已使用到之號碼")
	public Long isGetNumberSerial(@PathVariable String systemType, @PathVariable String branch);

	// 取得外匯編號 FXNO
	@PutMapping("/{noCode}/{systemType}/{branch}/get-fx-no")
	@Operation(description = "取得外匯編號", summary = "取得外匯編號並更新取號檔")
	public String getFxNo(@PathVariable String noCode, @PathVariable String systemType, @PathVariable String branch);
	
	// 取得IRCase seqNo
	@PutMapping("/get-ir-seq-no")
	@Operation(description = "取得匯入IRCase SEQ_NO", summary = "取得匯入IRCase SEQ_NO並更新取號檔")
	public String getSeqNo();
	
	// 顧客資料處理
	@GetMapping("/customer-account/{accountNumber}")
	@Operation(description = "以顧客帳號讀取顧客資料", summary = "依帳號讀取顧客資料")
	public Response<CustomerDto> getCustomer(@PathVariable String accountNumber);

	@GetMapping("/customerid/{customerId}")
	@Operation(description = "以顧客ID讀取顧客資料", summary = "依ID讀取顧客資料")
	public Response<CustomerDto> getCustomerId(@PathVariable String customerId);

	// 分行資料處理
	@GetMapping("/{branch}/get-branch-code")
	@Operation(description = "傳入分行號碼取得分行字軌", summary = "以分行代號取得分行字軌")
	public String getBranchCode(@PathVariable String branch);

	// 讀銀行檔
	@GetMapping("/bank/{swiftCode}")
	@Operation(description = "傳入SwiftCode查詢銀行檔", summary = "以SwiftCode查詢銀行檔")
	public BankDto getBank(@PathVariable String swiftCode);
	
	// TCTYR02 以匯款行/付款行國家代號查詢名稱
	@GetMapping("/bank/countryname/{countrycode}")
	@Operation(description = "傳入CountryCode查詢國家名稱", summary = "以CountryCode查詢國家名稱")
	public String getCountryName(@PathVariable("countrycode") String countryCode);

	// TBNMR12 依劃帳行ID+幣別代碼 查詢劃帳行名稱地址
	@GetMapping("/bank/countryadd/{swiftcode}/{currency}")
	@Operation(description = "傳入劃帳行ID+幣別代碼查詢劃帳行名稱地址", summary = "以劃帳行ID+幣別代碼查詢劃帳行名稱地址")
	public BankAddressDto getBankAdd(@PathVariable String swiftCode, @PathVariable String currency);

	// TBNMR13 依劃帳行ID 查詢劃帳行名稱地址 (幣別代碼=99)
	@GetMapping("/bank/countryadd/{swiftcode}/99")
	@Operation(description = "傳入劃帳行ID+99查詢劃帳行名稱地址", summary = "以劃帳行ID+99查詢劃帳行名稱地址")
	public Response<BankAddressDto> getBankAdd(@PathVariable String swiftCode);

	// 查詢error code
	@GetMapping("/errorcode/{errorcode}")
	@Operation(description = "傳入errorCode查詢錯誤說明", summary = "以errorCode查詢錯誤說明")
	public String getErrorMessage(@PathVariable("errorcode") String errorCode);

	// 手續費計算
	@GetMapping("/GetChargeFeeTWD")
	@Operation(description = "依currency, amount取得chargeFee(新台幣)", summary = "手續費計算")
	public BigDecimal isGetChargeFeeTWD(@PathVariable String currency, @PathVariable BigDecimal amount);

	// 讀取匯款性質名稱
	@GetMapping("/GetRemitNature")
	@Operation(description = "依remitNatureCode, remitNatureType取得remitNatureName", summary = "讀取匯款性質名稱")
	public String isGetRemitNature(@PathVariable String remitNatureCode, @PathVariable String remitNatureType);

}



