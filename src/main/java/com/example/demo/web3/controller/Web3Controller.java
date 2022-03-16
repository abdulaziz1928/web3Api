package com.example.demo.web3.controller;
import com.example.demo.web3.services.SwapService;
import com.example.demo.web3.services.Web3Service;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.RawTransaction;
import java.io.IOException;

@RestController
@RequestMapping(path = "api/v1/web3")
public class Web3Controller {

    private final Web3Service web3Service;
    private final SwapService swapService;

    @Autowired
    public Web3Controller(Web3Service web3Service,SwapService swapService) {
        this.web3Service = web3Service;
        this.swapService=swapService;
    }

    @GetMapping("/address/balance")
    public JSONObject getBalance(@RequestBody JSONObject  address){
        return  web3Service.getBalance(address);
    }

    @GetMapping("/{tokenContract}/balance")
    public JSONObject getERC20Balance(@RequestBody JSONObject  address,@PathVariable String tokenContract) throws Exception {
        return web3Service.getERC20Balance(address,tokenContract);
    }

    @PostMapping("/tx")
    public RawTransaction sendTransaction(@RequestBody JSONObject input) throws IOException {
        return  web3Service.sendTransaction(input);
    }
    @PostMapping("/tx/send")
    public String sendSignedTransaction(@RequestBody JSONObject input) throws Exception {
        return  web3Service.sendSignedTransaction(input);
    }
    @PostMapping("/tx/{tokenContract}/send")
    public String sendSignedERC20Transaction(@RequestBody JSONObject input,@PathVariable String tokenContract) throws Exception {
        return  web3Service.sendSignedERC20Transaction(input,tokenContract);
    }
    @GetMapping("/ethrate")
    public JSONObject testContract(@RequestBody JSONObject input) throws Exception {
        return swapService.getExchangeRate(input);
    }
    @GetMapping("/price")
    public String getPrice(@RequestBody JSONObject input) throws Exception {
        return swapService.getPrice(input);
    }
    @GetMapping("/swap/ethtoken")
    public String swapExactEthForToken(@RequestBody JSONObject input) throws Exception {
        return swapService.swapExactEthForToken(input);
    }

}
