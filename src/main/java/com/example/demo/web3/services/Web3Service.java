package com.example.demo.web3.services;
import com.example.demo.web3.models.MyERC20;
import io.reactivex.disposables.Disposable;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class Web3Service {
    private static final Web3j client= Web3j.build(new HttpService("https://rinkeby.infura.io/v3/a9fadbdf4f204dd3aa9b733691763878"));

    public JSONObject getBalance(JSONObject address) {
        try {
            final EthGetBalance balance= client.ethGetBalance(address.get("address").toString(), DefaultBlockParameter.valueOf("latest")).sendAsync()
                    .get(30, TimeUnit.SECONDS);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("balance", balance.getBalance().toString());
            return jsonObject;

        }catch (Exception e){System.out.println(e);}
        return null;
    }

    public JSONObject getERC20Balance(JSONObject address, String tokenContract) throws Exception {

        ClientTransactionManager transactionManager= new ClientTransactionManager(client,address.get("address").toString());
        ERC20 contract = ERC20.load(tokenContract, client, transactionManager, new DefaultGasProvider());
        BigInteger balance = contract.balanceOf(address.get("address").toString()).send();
        String tokenName= contract.name().send();
        String tokenSymbol= contract.symbol().send();
        JSONObject balanceJson = new JSONObject();
        balanceJson.put("address",address.get("address").toString());
        balanceJson.put("balance",balance.toString());
        balanceJson.put("token",tokenName);
        balanceJson.put("token_symbol",tokenSymbol);
//        balanceJson.put("txs", List.of());

        return balanceJson;
    }
    public  JSONObject subscribeERC20Wallet(JSONObject address, String tokenContract){
        ClientTransactionManager transactionManager= new ClientTransactionManager(client,address.get("address").toString());
        MyERC20 contract = MyERC20.load(tokenContract, client, transactionManager, new DefaultGasProvider());
        Disposable s=contract.transferEventFlowable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST).subscribe(tx ->{
            String toAddress = tx.to;
            String fromAddress = tx.from;
            String txHash = tx.log.getTransactionHash();
        });

        return new JSONObject();
    }

    public RawTransaction sendTransaction(JSONObject input) throws IOException {

        EthGetTransactionCount ethGetTransactionCount= client.ethGetTransactionCount(
                input.get("from").toString(), DefaultBlockParameter.valueOf("latest")
        ).send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
        String to= input.get("to").toString();
        String amount= input.get("amount").toString();
        BigInteger value= new BigInteger(amount);
        DefaultGasProvider gasProvider= new DefaultGasProvider();
        BigInteger gasLimit= gasProvider.getGasLimit();
        BigInteger gasPrice= gasProvider.getGasPrice();
        RawTransaction tx = RawTransaction.createEtherTransaction(nonce,gasPrice,gasLimit,to,value);


        return tx;
    }

    public JSONObject getFees() throws IOException {
        DefaultGasProvider dgp=new DefaultGasProvider();
        EthGasPrice s=client.ethGasPrice().send();
        BigInteger fees=dgp.getGasLimit().multiply(s.getGasPrice());

        JSONObject res= new JSONObject();
        res.put("max_fee",fees.toString());
        return res;
    }

    public String sendSignedTransaction(JSONObject input) throws Exception {

        String to= input.get("to").toString();
        String amount= input.get("amount").toString();
        BigInteger valueB= new BigInteger(amount);
        Long value= valueB.longValue();

        Credentials credentials= Credentials.create(input.get("private").toString());
        TransactionReceipt receipt = Transfer.sendFunds(client, credentials, to, BigDecimal.valueOf(value), Convert.Unit.WEI).send();

        return receipt.getTransactionHash();
    }

    public String sendSignedERC20Transaction(JSONObject input,String tokenContract) throws ExecutionException, InterruptedException {

        String from=input.get("from").toString();
        String to= input.get("to").toString();
        String amount= input.get("amount").toString();
        BigInteger valueB= new BigInteger(amount);
        Credentials credentials= Credentials.create(input.get("private").toString());

        ERC20 contract = ERC20.load(tokenContract,client,credentials, new DefaultGasProvider());
        contract.approve(from,valueB);
        TransactionReceipt receipt = contract.transfer(to,valueB).sendAsync().get();
        return receipt.getTransactionHash();
    }



}
