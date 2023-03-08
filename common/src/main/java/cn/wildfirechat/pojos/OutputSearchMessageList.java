/*
 * This file is part of the Wildfire Chat package.
 * (c) Heavyrain2012 <heavyrain.lee@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package cn.wildfirechat.pojos;

import java.util.List;

public class OutputSearchMessageList {
    private List<PojoSearchMessage> messages;

    public List<PojoSearchMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<PojoSearchMessage> messages) {
        this.messages = messages;
    }
}
