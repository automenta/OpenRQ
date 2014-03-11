package net.fec.openrq;


import net.fec.openrq.encoder.DataEncoderBuilder;
import net.fec.openrq.encoder.DataEncoder;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class Builders {

    static DataEncoderBuilder newEncoderBuilder(byte[] data) {

        return new ArrayEncBuilder(data);
    }


    private static final class ArrayEncBuilder extends AbstractEncBuilder {

        private final byte[] data;


        ArrayEncBuilder(byte[] data) {

            this.data = data;
        }

        @Override
        public DataEncoder build() {

            // TODO derive T, Z, N, Al and return encoder instance
            return null;
        }

    }

    private static abstract class AbstractEncBuilder implements DataEncoderBuilder {

        protected int maxPayload;
        protected int maxDecBlock;
        protected int minSubSymbol;


        protected AbstractEncBuilder() {

            defaultMaxPayload();
            defaultMaxDecoderBlock();
            defaultMinSubSymbol();
        }

        @Override
        public DataEncoderBuilder maxPayload(int maxPayloadLen) {

            if (maxPayloadLen <= 0) throw new IllegalArgumentException("non-positive maxPayloadLen");
            this.maxPayload = maxPayloadLen;
            return this;
        }

        @Override
        public DataEncoderBuilder defaultMaxPayload() {

            this.maxPayload = DataEncoderBuilder.DEF_MAX_PAYLOAD_LENGTH;
            return this;
        }

        @Override
        public DataEncoderBuilder maxDecoderBlock(int maxBlock) {

            if (maxBlock <= 0) throw new IllegalArgumentException("non-positive maxBlock");
            this.maxDecBlock = maxBlock;
            return this;
        }

        @Override
        public DataEncoderBuilder defaultMaxDecoderBlock() {

            this.maxDecBlock = DataEncoderBuilder.DEF_MAX_DEC_BLOCK_SIZE;
            return this;
        }

        @Override
        public DataEncoderBuilder minSubSymbol(int minSubSymbol) {

            if (minSubSymbol <= 0) throw new IllegalArgumentException("non-positive minSubSymbol");
            this.minSubSymbol = minSubSymbol;
            return this;
        }

        @Override
        public DataEncoderBuilder defaultMinSubSymbol() {

            this.minSubSymbol = DataEncoderBuilder.DEF_MIN_SUB_SYMBOL;
            return this;
        }

        @Override
        public abstract DataEncoder build();

    }


    private Builders() {

        // not instantiable
    }
}
