package tw.com.fcb.dolala.core.common.web;

import java.math.BigDecimal;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import tw.com.fcb.dolala.core.common.http.Response;
import tw.com.fcb.dolala.core.common.repository.entity.SerialNumber;
import tw.com.fcb.dolala.core.common.service.*;
import tw.com.fcb.dolala.core.common.service.vo.FpmVo;
import tw.com.fcb.dolala.core.common.web.dto.*;
import tw.com.fcb.dolala.core.common.service.CountryService;
import tw.com.fcb.dolala.core.common.service.CustomerAccountService;
import tw.com.fcb.dolala.core.common.service.CustomerService;
import tw.com.fcb.dolala.core.common.service.ExchgRateService;
import tw.com.fcb.dolala.core.common.service.IDNumberCheckService;
import tw.com.fcb.dolala.core.common.service.vo.BankVo;
import tw.com.fcb.dolala.core.config.IRConfig;
import tw.com.fcb.dolala.core.mq.Runner;

import javax.validation.constraints.NotNull;

/**
 * @author sinjen
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController implements CommonApi {

    @Autowired
    IRConfig irConfig;
    @Autowired
    ExchgRateService fxService;
    @Autowired
    CountryService countryService;
    @Autowired
    IDNumberCheckService idNumberCheckService;
    @Autowired
    SerialNumberService serialNumberService;
    @Autowired
    BankService bankService;
    @Autowired
    CustomerAccountService customerAccountService;
    @Autowired
    CustomerService customerService;
    @Autowired
    BranchCheckService branchCheckService;
    @Autowired
    ErrorMessageService errorMessageService;
    @Autowired
    ChargeFeeCalculateService chargeFeeCalculateService;
    @Autowired
    RemitNatureService remitNatureService;
    @Autowired
    FpService fpService;


    // 匯率處理
    @Override
    public BigDecimal isGetFxRate(@PathVariable("exchg-rate-type") String exchgRateType,
                                  @PathVariable("currency") String currency,
                                  @PathVariable("standard-currency") String standardCurrency) {
        log.info("${env-type} = {}", irConfig.getEnvType());
        BigDecimal exchangeRate = fxService.getRate(exchgRateType, currency, standardCurrency);
        log.info("呼叫讀取匯率API：取得ExchgRate = " + exchangeRate);
        return exchangeRate;
    }

    public boolean isCheckFxRate(BigDecimal exchgRate) {
        log.info("呼叫匯率處理API：檢核承作匯率是否超過權限範圍");
        log.info("呼叫匯率處理API：檢核承作匯率是否超出合理範圍");
        return true;
    }

    // 國家資料處理
    public String isGetCountryNumber(@PathVariable("countryCode") String countryCode) {
        String countryNumber = null;
        countryNumber = countryService.getCountryNumber(countryCode);
        log.info("呼叫國別處理API：以國家代號2碼:" + countryCode + " 讀取國家代號4碼:" + countryNumber);
        return countryNumber;
    }

    public String isGetCountryCode(@PathVariable("countryNumber") String countryNumber) {
        String countryCode = null;
        countryCode = countryService.getCountryCode(countryNumber);
        log.info("呼叫國別處理API：以國家代號4碼:" + countryNumber + " 讀取國家代號2碼:" + countryCode);
        return countryCode;
    }

    // 身分證號檢核
    public boolean isCheckId(@PathVariable("id") String id) {
        boolean check = false;
        check = idNumberCheckService.isValidIDorRCNumber(id);
        log.info("呼叫身分證號檢核API：檢核" + id + "是否符合編碼規則:" + check);
        return check;
    }

    // 讀取取號檔
    public Long isGetNumberSerial(@PathVariable("systemType") String systemType, @PathVariable("branch") String branch) {
        SerialNumber serialNumber = serialNumberService.getNumberSerial(systemType, branch);
        log.info("呼叫讀取取號檔API：查詢" + systemType + "現已使用到第" + serialNumber.getSerialNo() + "號");
        return serialNumber.getSerialNo();
    }

    // 取得外匯編號 FXNO
    public String getFxNo(@PathVariable("noCode") String noCode, @PathVariable("systemType") String systemType, @PathVariable("branch") String branch) {
        String fxNo = null;
        try {
            fxNo = serialNumberService.getFxNo(systemType, branch);
            String numberSerial = null;
            if (branch.equals("093")) {
                numberSerial = fxNo.substring(6, 10);
                log.info("呼叫取得外匯編號API：取得serialno = " + fxNo.substring(6, 10));
            } else {
                numberSerial = fxNo.substring(5, 10);
            }
            serialNumberService.updateSerialNumber(systemType, branch, Long.valueOf(numberSerial));
            log.info("呼叫取得外匯編號API：FXNO = " + fxNo + ", 並更新取號檔成功 = " + numberSerial);

        } catch (Exception e) {
            log.info("取得外匯編號錯誤" + e);
        }
        return fxNo;
    }

    // 取得IRCase seqNo
    public String getSeqNo() {
        String irSeq = null;

        final String branch = "999";

        final String systemType = "IR_SEQ";
        try {
            irSeq = serialNumberService.getIrSeqNo(systemType, branch);

            serialNumberService.updateSerialNumber(systemType, branch, Long.valueOf(irSeq));
            log.info("呼叫取得IRCase SEQ_NO API：SEQ_NO = " + irSeq + ", 並更新取號檔成功");

        } catch (Exception e) {
            log.info("取得SEQ_NO錯誤" + e);
        }
        return irSeq;
    }


    //顧客資料處理
    public Response<CustomerDto> getCustomer(@NotNull @PathVariable("accountNumber") String accountNumber) {
        log.info("接收accountNumber = " + accountNumber);
        CustomerDto customerDto = null;
        Response<CustomerDto> response = new Response<CustomerDto>();
        CustomerAccountDto customerAccountDto = null;
        try {
            customerAccountDto = customerAccountService.getCustomerAccount(accountNumber);
            log.info("呼叫讀取顧客帳戶API：顧客帳戶資料：" + customerAccountDto.toString());

            customerDto = customerService.getCustomer(customerAccountDto.getCustomerSeqNo());
            log.info("呼叫讀取顧客檔API：顧客資料：" + customerDto.toString());
            response.Success();
        } catch (Exception e) {
            log.info(String.valueOf(e));
        }
        response.setData(customerDto);
        return response;

    }

    public Response<CustomerDto> getCustomerId(@NotNull @PathVariable("customerId") String customerId) {
        log.info("接收accountId = " + customerId);
        CustomerDto customerDto = null;
        Response<CustomerDto> response = new Response<CustomerDto>();

        try {
            customerDto = customerService.getCustomerId(customerId);
            log.info("呼叫讀取顧客檔API：顧客資料：" + customerDto.toString());
            response.Success();
        } catch (Exception e) {
            log.info(String.valueOf(e));
            response.Error(e.getMessage(), getErrorMessage(e.getMessage()));
        }
        response.setData(customerDto);
        return response;
    }

    // 分行資料處理
    public String getBranchCode(@NotNull @PathVariable("branch") String branch) {
        String branchCode = null;
        try {
            branchCode = branchCheckService.getBranchCode(branch);
            log.info("呼叫取得分行字軌API：branch = " + branch + "字軌 = " + branchCode);
        } catch (Exception e) {
            log.info("讀取branchService錯誤" + e);
        }
        return branchCode;
    }

    // 讀銀行檔
    public BankDto getBank(@NotNull @PathVariable("swiftCode") String swiftCode) {
        BankDto bankDto = new BankDto();
        BankVo bankVo = new BankVo();
        try {
            bankVo = bankService.findBySwiftCode(swiftCode);
            BeanUtils.copyProperties(bankVo, bankDto);
            log.info("呼叫取得銀行檔API：取得swiftCode = " + swiftCode + "之銀行檔");
        } catch (Exception e) {
            log.info(String.valueOf(e));
        }
        return bankDto;
    }

    // TCTYR02 以匯款行/付款行國家代號查詢名稱
    public String getCountryName(@NotNull @PathVariable("countrycode") String countryCode) {

        log.info("呼叫讀取國家名稱API：查詢 " + countryCode);

        return "CountryName";
    }

    // TBNMR12 依劃帳行ID+幣別代碼 查詢劃帳行名稱地址
    public BankAddressDto getBankAdd(@PathVariable("swiftcode") String swiftCode, @PathVariable("currency") String currency) {
        BankAddressDto bankAddressDto = new BankAddressDto();

        log.info("呼叫劃帳行名稱地址API：查詢 " + swiftCode + "+" + currency);

        bankAddressDto.setName("ABank");
        bankAddressDto.setAddress("No.1, X st.,Tapei City 106");
        return bankAddressDto;
    }

    // TBNMR13 依劃帳行ID 查詢劃帳行名稱地址 (幣別代碼=99)
    public Response<BankAddressDto> getBankAdd(@PathVariable("swiftcode") String swiftCode) {
        BankAddressDto bankAddressDto = new BankAddressDto();
        BankVo bankVo = new BankVo();
        Response<BankAddressDto> response = new Response<BankAddressDto>();

        try {
            bankVo = bankService.findBySwiftCode(swiftCode);
            BeanUtils.copyProperties(bankVo, bankAddressDto);
            log.info("呼叫劃帳行名稱地址API：查詢 " + swiftCode + "+" + 99);
            response.Success();
        } catch (Exception e) {
            log.info(String.valueOf(e));
            response.Error(e.getMessage(), getErrorMessage(e.getMessage()));
        }
        response.setData(bankAddressDto);
        return response;
    }

    // 查詢error code
    public String getErrorMessage(@PathVariable("errorcode") String errorCode) {
        String errorMessage = null;
        try {
            errorMessage = errorMessageService.findByErrorCode(errorCode);
        } catch (Exception e) {
            errorMessage = "查無錯誤訊息";
        }

        return errorMessage;
    }

    // 手續費計算
    public BigDecimal isGetChargeFeeTWD(String currency, BigDecimal amount) {
        BigDecimal chargeFee = null;
        chargeFee = chargeFeeCalculateService.chargeFeeTWDCalculat(currency, amount);
        log.info("呼叫手續費計算API：輸入幣別" + currency + " 金額" + amount + " 取得新台幣手續費=" + chargeFee);
        return chargeFee;
    }

    // 讀取匯款性質名稱
    public String isGetRemitNature(String remitNatureCode, String remitNatureType) {
        String remitNatureName = null;
        remitNatureName = remitNatureService.getRemitNature(remitNatureCode, remitNatureType);
        log.info("呼叫匯款性質代碼API：輸入申報性質代碼" + remitNatureCode + " 匯款性質分類" + remitNatureType + " 取得申報性質名稱=" + remitNatureName);
        return remitNatureName;
    }

    public Response<FpmDto> getByFpmCurrency(String account, String crcy) {
        FpmDto fpmDto = new FpmDto();
        FpmVo fpmVo = new FpmVo();
        Response<FpmDto> response = new Response<FpmDto>();
        try {
            fpmVo = fpService.getByFpmCurrency(account, crcy);
            BeanUtils.copyProperties(fpmVo, fpmDto);
            log.info("呼叫Fpm API：查詢 " + account + crcy);
            response.setData(fpmDto);
            response.Success();
        } catch (Exception e) {
            log.info(String.valueOf(e));
            response.Error(e.getMessage(), getErrorMessage(e.getMessage()));
        }

        return response;
    }

    public Response<Integer> updateFpmBalance(String account, String crcy, BigDecimal amt) {FpmDto fpmDto = new FpmDto();
        Response<Integer> response = new Response<Integer>();
        try {
            Integer result = fpService.updateFpmBalance(account, crcy, amt);
            log.info("呼叫Fp 入帳API：輸入" + account + "+" + crcy + "+" + amt);
            response.setData(result);
            response.Success();
        } catch (Exception e) {
            log.info(String.valueOf(e));
            response.Error(e.getMessage(), getErrorMessage(e.getMessage()));
        }

        return response;
    }


}
