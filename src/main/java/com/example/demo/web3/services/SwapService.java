package com.example.demo.web3.services;
import com.example.demo.web3.models.*;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.List;

@Service
public class SwapService {
    private static final Web3j client= Web3j.build(
            new HttpService("https://eth-rinkeby.alchemyapi.io/v2/F5yu5yvHjAWiRJWLugIWwlJaiW-n4dM7"));
//    private static final Web3j client= Web3j.build(
//            new HttpService("https://rinkeby.infura.io/v3/a9fadbdf4f204dd3aa9b733691763878"));


    //working
    public JSONObject getExchangeRate(JSONObject input) throws Exception {

        String routerContractAddress="0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D";
        String token1= input.get("token1").toString();
        String privateKeys= input.get("private").toString();
        String amount= input.get("amount_token0").toString();

        Credentials credentials= Credentials.create(privateKeys);

        UniswapV2Router02 contractRouter= UniswapV2Router02.load(routerContractAddress,client,credentials,new DefaultGasProvider());
        String token0=contractRouter.WETH().send();
        List r=contractRouter.getAmountsOut(new BigInteger(amount),List.of(token0,token1)).send();

        JSONObject j=new JSONObject();
        j.put("pair",r);
        j.put("formula_to_use","(token0Amount * 10 ^ token0Decimals) / (token1Amount * 10 ^ token0Decimals)");
        return j;
    }

    //Working
    public String swapExactEthForToken(JSONObject input) throws Exception {
        String routerContractAddress="0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D";
        String token1= input.get("token_address").toString();
        String privateKeys= input.get("private").toString();
        Credentials credentials= Credentials.create(privateKeys);
        String amnt= input.get("amount_eth").toString();
        BigInteger amount= new BigInteger(amnt);

        UniswapV2Router02 contractRouter= UniswapV2Router02.load(routerContractAddress,client,credentials,new DefaultGasProvider());

        List amountsout= contractRouter.getAmountsOut(new BigInteger(amnt),List.of(contractRouter.WETH().send(),token1)).send();
        double samountOutMin= Double.parseDouble(amountsout.get(1).toString()) * 90 / 100;
        long m= ((long) samountOutMin);
        BigInteger amountOutMin=new BigInteger(String.valueOf(m));
                System.out.println(new BigInteger(String.valueOf(m)));
        System.out.println(amountsout);
        System.out.println(amountOutMin);

        return contractRouter.swapExactETHForTokens(amountOutMin,List.of(contractRouter.WETH().send(),token1),credentials.getAddress(), BigInteger.valueOf((System.currentTimeMillis()/1000)+60*10),amount).sendAsync().get().toString();
    }


    //ChainLink Price Feeds Input PrivateKey + Price Feed Contract Address
    public String getPrice(JSONObject input) throws Exception {
        String contractAddress="0x5f4eC3Df9cbd43714FE2740f5E3616155c5b8419";
        String privateKeys= input.get("private").toString();
        Credentials credentials= Credentials.create(privateKeys);


        AggregatorV3Interface contract= AggregatorV3Interface.load(contractAddress,client,credentials,new DefaultGasProvider());

        Tuple5<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> s= contract.latestRoundData().send();
        return s.toString();
    }




}
