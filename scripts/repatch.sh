#
# Copyright (c) 2018-2021 Karlatemp. All rights reserved.
# @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
#
# LuckPerms-Mirai/LuckPerms-Mirai/repatch.sh
#
# Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
#
# https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
#

cd LuckPerms || exit

git reset --hard HEAD
git apply --reject ../patchs.patch
tmerr=$?

cd ..
exit $tmerr
