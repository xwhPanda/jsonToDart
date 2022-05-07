import com.android.annotations.NonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import javax.swing.*;
import java.awt.event.*;

public class InputDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel classNameLabel;
    private JTextArea jsonArea;
    private JTextField classNameField;
    private JLabel jsonLabel;
    private JButton formatJson;
    private JLabel resultLabel;
    private ClickOkListener listener;

    public InputDialog(ClickOkListener listener) {
        this.listener = listener;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resultLabel.setText("");
                if(classNameField.getText().trim().isBlank()){
                    resultLabel.setText("class name error !!!");
                    return;
                }
                if (!checkJson()){
                    resultLabel.setText("json data error !!!");
                    return;
                }

                listener.onClick(classNameField.getText(),jsonArea.getText());
                dispose();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        formatJson.addActionListener(e -> {
            resultLabel.setText("");
            if (!checkJson()){
                resultLabel.setText("json data error !!!");
            } else {
                try {
                    jsonArea.setText(JsonFormatUtils.INSTANCE.formatJson(jsonArea.getText()));
                } catch (JsonProcessingException jsonProcessingException) {
                    resultLabel.setText("json format error !!!");
                }
            }
        });

//        jsonArea.setText("{\"id\":\"216099\",\"orderNo\":\"1511901730542063618\",\"mainOrderNo\":\"1511901730491731968\",\"status\":10,\"orderType\":1,\"shopName\":\"实得五金\",\"shopCode\":\"SH00000451\",\"shouldAmount\":155.3600,\"skuAmount\":147.3600,\"canRefund\":null,\"canReturnRefund\":null,\"activityOrderName\":null,\"redPacketAmount\":0.0000,\"pointsDeduction\":0.0000,\"freightPrice\":8.0000,\"couponAmount\":0.0000,\"placeTime\":\"2022-04-07 11:00:37\",\"finishTime\":null,\"refundDeadline\":null,\"paymentDeadline\":\"2022-04-07 11:30:37\",\"payTime\":null,\"customerName\":\"NullPointerException\",\"invoiceStatus\":0,\"invoiceStatusDesc\":\"未开票\",\"lastestLogisticsDesc\":null,\"lastestLogisticsSendTime\":null,\"statusDesc\":\"已关闭\",\"shopType\":2,\"allFinished\":null,\"isNeedInsteadDelivery\":false,\"vipOrder\":false,\"payManner\":14,\"collectionStatusStr\":null,\"collectionStatus\":null,\"sendTime\":null,\"receiveTime\":null,\"buyerRemark\":null,\"lastSendTime\":null,\"customerMobile\":\"15721476718\",\"pcOrderActionVOS\":null,\"isCreditPay\":null,\"subOrderSource\":\"\",\"nowTime\":\"2022-04-29 16:57:12\",\"orderAddress\":{\"address\":\"广东省广州市海珠区新港街道测试\",\"consigneeName\":\"安徽籍\",\"consigneeMobile\":\"15732837329\",\"provinceName\":\"广东省\",\"cityName\":\"广州市\",\"districtName\":\"海珠区\",\"streetName\":\"新港街道\"},\"orderActivityDTOS\":null,\"orderLogisticsVOS\":null,\"orderDetailVOS\":[{\"id\":\"437222\",\"orderDetailNo\":\"1511901730542063619\",\"orderNo\":\"1511901730542063618\",\"spuCode\":\"SH0000045100035\",\"skuCode\":\"SH000004510003500001\",\"skuName\":\"美特直钉\",\"skuImg\":\"https://qiniu-cdn.yigongpin.com/goods/image/2022-03-14/5f80de1da43b4e6da2c4a443ac8079c4.jpg\",\"skuAttrVal\":\"F30（100*50）\",\"modelCode\":\"F30（100*50）\",\"brand\":\"美特\",\"skuTotalAmount\":147.3600,\"shouldAmount\":155.3600,\"pricePerItem\":147.3600,\"freightPrice\":8.0000,\"refundPrice\":0.0000,\"pointsDeduction\":0.0000,\"refundPointsDeduction\":0.0000,\"itemPcs\":\"盒\",\"skuUnitPrice\":12.2800,\"buyNum\":12,\"serviceNum\":0,\"deliveryNum\":0,\"cancelNum\":0,\"signNum\":0,\"allowInvoice\":0,\"shopType\":2,\"num\":12,\"onlyRefundNum\":null,\"returnRefundNum\":null,\"deliveryStatus\":0,\"deliveryStatusDesc\":\"待发货\",\"signStatus\":0,\"deliveryTime\":null,\"receiveTime\":null,\"refundAmount\":0,\"redPacketPrice\":0.0000,\"allowInvoiceDesc\":\"\"}],\"employeeName\":\"NullPointerException\",\"millisecAllowToPay\":\"-1920395788\"}");



        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * 校验json
     * @return
     */
    private boolean checkJson(){
        if (jsonArea.getText().isEmpty()){
            return false;
        }
        try {
            JsonElement jsonElement = JsonParser.parseString(jsonArea.getText());
            if ((jsonElement.isJsonObject() || jsonElement.isJsonArray())){
                return true;
            } else {
                return false;
            }

        } catch (JsonSyntaxException exception) {
            return false;
        }
    }
    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    interface ClickOkListener{
        void onClick(@NonNull String className, @NonNull String json);
    }

    public static void main(String[] args) {
//        InputDialog dialog = new InputDialog();
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
    }
}
